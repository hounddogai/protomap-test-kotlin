package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalQualificationMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalQualificationMilestoneStore()
        val item = newSeasonalQualificationMilestone("code-705", "label-971")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
