package com.acme.transport.kit.ids

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdsTest {
    @Test
    fun `pads sequence numbers`() {
        assertEquals("INV-000042", newId("inv", 42), "id")
        assertTrue(isReference("INV-000042", "inv"), "reference")
    }
}
