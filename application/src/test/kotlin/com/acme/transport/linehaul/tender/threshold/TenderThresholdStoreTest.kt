package com.acme.transport.linehaul.tender.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderThresholdStore()
        val item = newTenderThreshold("code-796", "name-796")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
