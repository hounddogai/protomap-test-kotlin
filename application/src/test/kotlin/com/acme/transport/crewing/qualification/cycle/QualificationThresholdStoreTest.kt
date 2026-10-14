package com.acme.transport.crewing.qualification.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationThresholdStore()
        val item = newQualificationThreshold("code-455", "name-256")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
