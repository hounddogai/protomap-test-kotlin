package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackQualificationMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackQualificationMilestoneStore()
        val item = newFallbackQualificationMilestone("code-66", "title-183")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
