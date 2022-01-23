package com.example.eyesup;

import com.example.eyesup.model.Cafe;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


public class CafeTest {


    @Test
    public void testGetPhone() {
        Cafe c = new Cafe(5000, "coffe", "938363255", 3.3, 3.2);
        assertEquals(c.getPhone(),"938363255");
    }

    @Test
    public void testGetName() {
        Cafe c = new Cafe(5000, "coffe", "938363255", 3.3, 3.2);
        assertEquals(c.getName(),"coffe");
    }
}