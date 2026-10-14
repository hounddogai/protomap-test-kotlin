package com.acme.transport.fleetops.tire.tireagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryTireGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryTireGroupStore()
        val item = newPrimaryTireGroup("code-452", "name-166")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
