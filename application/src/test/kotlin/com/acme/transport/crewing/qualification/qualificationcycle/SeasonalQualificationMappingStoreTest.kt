package com.acme.transport.crewing.qualification.qualificationcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalQualificationMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalQualificationMappingStore()
        val item = newSeasonalQualificationMapping("code-407", "title-445")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
