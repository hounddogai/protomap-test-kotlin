package com.acme.transport.emissions.evcharge.batchedchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryEvChargeGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryEvChargeGroupStore()
        val item = newPrimaryEvChargeGroup("code-238", "name-766")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
