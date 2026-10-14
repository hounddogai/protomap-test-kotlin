package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictBackhaulVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictBackhaulVarianceStore()
        val item = newStrictBackhaulVariance("code-19", "name-745")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
