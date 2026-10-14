package com.acme.transport.linehaul.tender.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveTenderWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveTenderWindowStore()
        val item = newAdaptiveTenderWindow("code-275", "title-567")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
