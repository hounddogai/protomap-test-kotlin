package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDefectVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDefectVarianceStore()
        val item = newFallbackDefectVariance("code-730", "label-473")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
