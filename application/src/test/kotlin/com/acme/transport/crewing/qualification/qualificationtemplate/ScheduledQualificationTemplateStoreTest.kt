package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledQualificationTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledQualificationTemplateStore()
        val item = newScheduledQualificationTemplate("code-289", "label-947")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
