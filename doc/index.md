# Index

Index a title database saved in a .tsv file. You should put the file path as a query.

**URL** : `/index`

**Method** : `POST`

**URL Params**

 * `path=[string]` (Required)

**Auth required** : No

**Permissions required** : None

**Request example**

`/index?path=/Users/alvaro/Downloads/data.tsv`

## Success Response

**Condition** : If indexing process started correctly.

**Code** : `200 OK`
<!-- Write later
## Error Responses

**Condition** : If Account already exists for User.

**Code** : `303 SEE OTHER`

**Headers** : `Location: http://testserver/api/accounts/123/`

**Content** : `{}`

### Or

**Condition** : If fields are missed.

**Code** : `400 BAD REQUEST`

**Content example**

```json
{
    "name": [
        "This field is required."
    ]
}
``` -->