package com.acme.transport.crewing.qualification.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationEstimateStore()
        val item = newQualificationEstimate("code-185", "name-369")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
