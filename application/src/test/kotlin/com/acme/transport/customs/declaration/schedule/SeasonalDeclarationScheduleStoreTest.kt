package com.acme.transport.customs.declaration.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDeclarationScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDeclarationScheduleStore()
        val item = newSeasonalDeclarationSchedule("code-293", "label-624")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
