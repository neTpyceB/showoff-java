package com.showoff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserTest {
    @Test
    void user_gettersSettersToString() {
        User user = new User("Alice", 30);
        assertEquals("Alice", user.getName());
        assertEquals(30, user.getAge());
        assertEquals("User{name='Alice', age=30}", user.toString());

        user.setName("Bob");
        user.setAge(25);
        assertEquals("Bob", user.getName());
        assertEquals(25, user.getAge());
    }

    @Test
    void user_equalsAndHashCode() {
        User a1 = new User("Alice", 30);
        User a2 = new User("Alice", 30);
        User b = new User("Bob", 30);
        User c = new User("Alice", 31);

        assertEquals(a1, a2);
        assertEquals(a1, a1);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1, b);
        assertNotEquals(a1, c);
        assertNotEquals(a1, null);
        assertNotEquals(a1, "not-a-user");
    }

    @Test
    void user_invalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new User(null, 10));
        assertThrows(IllegalArgumentException.class, () -> new User("Alice", -1));

        User user = new User("Alice", 10);
        assertThrows(IllegalArgumentException.class, () -> user.setName(null));
        assertThrows(IllegalArgumentException.class, () -> user.setAge(-1));
    }

    @Test
    void userRecord_behavesAsValue() {
        UserRecord r1 = new UserRecord("Alice", 30);
        UserRecord r2 = new UserRecord("Alice", 30);
        UserRecord r3 = new UserRecord("Bob", 30);

        assertEquals("Alice", r1.name());
        assertEquals(30, r1.age());
        assertEquals(r1, r2);
        assertNotEquals(r1, r3);
        assertTrue(r1.toString().contains("UserRecord"));
    }

    @Test
    void userRecord_invalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new UserRecord(null, 10));
        assertThrows(IllegalArgumentException.class, () -> new UserRecord("Alice", -1));
    }
}
