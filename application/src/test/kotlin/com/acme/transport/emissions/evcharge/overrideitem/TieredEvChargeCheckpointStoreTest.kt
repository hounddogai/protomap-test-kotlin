package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredEvChargeCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredEvChargeCheckpointStore()
        val item = newTieredEvChargeCheckpoint("code-135", "name-202")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
