package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledQualificationThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledQualificationThresholdStore()
        val item = newScheduledQualificationThreshold("code-426", "name-747")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
