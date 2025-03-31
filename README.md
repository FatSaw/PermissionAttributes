### Config format:
- `<group_priority>` - hex byte, `00` used for `default` `<group_id>`, `FF` ignored
- `<attribute_name>` - String, attribute name
- `<attribute_value>` - double, attribute value
- `<group_id>` - String, used in permission `pa.<group_id>`
```
default:
 attributes:
  <attribute_name>: <attribute_value>
  <attribute_name>: <attribute_value>
groups:
 <group_id>: <group_priority>
  attributes:
   <attribute_name>: <attribute_value>
   <attribute_name>: <attribute_value>
 <group_id>: <group_priority>
  attributes:
   <attribute_name>: <attribute_value>
   <attribute_name>: <attribute_value>

```
