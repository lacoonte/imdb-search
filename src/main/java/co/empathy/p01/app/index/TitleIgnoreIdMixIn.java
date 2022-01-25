package co.empathy.p01.app.index;

import com.fasterxml.jackson.annotation.JsonIgnore;

record TitleIgnoreIdMixIn(@JsonIgnore String id) {
    
}
