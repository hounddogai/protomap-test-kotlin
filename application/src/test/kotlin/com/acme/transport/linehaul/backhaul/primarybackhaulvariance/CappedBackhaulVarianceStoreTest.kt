package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedBackhaulVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedBackhaulVarianceStore()
        val item = newCappedBackhaulVariance("code-168", "title-53")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
