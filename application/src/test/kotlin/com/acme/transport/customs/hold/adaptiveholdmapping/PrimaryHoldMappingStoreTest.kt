package com.acme.transport.customs.hold.adaptiveholdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryHoldMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryHoldMappingStore()
        val item = newPrimaryHoldMapping("code-267", "label-877")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
