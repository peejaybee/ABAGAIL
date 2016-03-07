class String
  def is_number?
    true if Float(self) rescue false
  end
end

require 'csv'

TRAINING_FRACTION = 0.7

count = 0
field_names = []
field_values = []
fields_to_skip = [2]
numeric_fields = [0, 4, 10, 11, 12] #these are supposed to be numbers

training_out = File.new 'adult_training.csv', 'w'
testing_out = File.new 'adult_testing.csv', 'w'


INPUT_FILENAME = '../src/opt/test/Adult-small.csv'

CSV.foreach INPUT_FILENAME do |row|

  row_out_data = []
  row.each_index do |i|
    if count == 0 #get field names from first row
      field_names << row[i]
      field_values << {}
    else
      if numeric_fields.include? i #don't translate columns that are numeric -- report any non-numerics i might not have seen
        if row[i].is_number?
          row_out_data << row[i]
        else
          puts "alleged number #{row[i]}"
          raise StandardError.new "Non-numeric value in numeric column #{i} line #{count + 1}"
        end
      else
        field_values[i][row[i]] = field_values[i].length if !field_values[i][row[i]]
        row_out_data << field_values[i][row[i]] if !fields_to_skip.include? i #replace with the translated value
      end

    end
  end
  if count > 0
    if Random.rand(1.0) < TRAINING_FRACTION
      training_out << row_out_data.join(',') + "\n"
    else
      testing_out << row_out_data.join(',') + "\n"
    end
  end
  count += 1
end
training_out.close
testing_out.close

fout = File.new 'key.txt', 'w'
field_names.each_index do |i|
  if !fields_to_skip.include? i
    fout.print "Field: #{field_names[i]}\n"
    field_values[i].each { |name, id| fout.print "#{name}: #{id}\n" }
    fout.print "----------\n"
  end

end

