package com.acme.transport.crewing.qualification.qualificationcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalQualificationCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalQualificationCycleStore()
        val item = newSeasonalQualificationCycle("code-632", "title-613")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
