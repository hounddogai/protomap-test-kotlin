package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationScheduleStore()
        val item = newQualificationSchedule("code-695", "label-183")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
