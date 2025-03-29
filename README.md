### Config format:
- `<priority>` - hex byte, `00` used for `default` `<attributes_id>`, `FF` ignored
- `<attribute_name>` - String, attribute name
- `<attribute_value>` - double, attribute value
- `<attributes_id>` - String, used in permission `pa.<attributes_id>`
```
attributes:
 <attributes_id>: <priority>
  <attribute_name>: <attribute_value>
  <attribute_name>: <attribute_value>
 <attributes_id>: <priority>
  <attribute_name>: <attribute_value>
  <attribute_name>: <attribute_value>

```
