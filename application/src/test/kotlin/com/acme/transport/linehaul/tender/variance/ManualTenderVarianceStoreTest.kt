package com.acme.transport.linehaul.tender.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTenderVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTenderVarianceStore()
        val item = newManualTenderVariance("code-930", "label-833")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
