package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRestBreakRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRestBreakRollupStore()
        val item = newTieredRestBreakRollup("code-979", "label-741")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
