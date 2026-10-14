package com.acme.transport.lastmile.drop.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDropThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDropThresholdStore()
        val item = newTieredDropThreshold("code-213", "name-849")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
