package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationMilestoneStore()
        val item = newQualificationMilestone("code-564", "name-235")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
