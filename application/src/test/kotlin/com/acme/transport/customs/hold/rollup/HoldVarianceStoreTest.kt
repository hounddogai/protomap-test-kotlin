package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldVarianceStore()
        val item = newHoldVariance("code-596", "name-371")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
