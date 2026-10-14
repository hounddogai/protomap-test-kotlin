package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderWindowStore()
        val item = newTenderWindow("code-602", "name-744")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
