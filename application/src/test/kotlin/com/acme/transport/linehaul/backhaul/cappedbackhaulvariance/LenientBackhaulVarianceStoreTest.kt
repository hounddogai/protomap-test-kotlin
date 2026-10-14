package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientBackhaulVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientBackhaulVarianceStore()
        val item = newLenientBackhaulVariance("code-357", "label-413")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
