package com.acme.transport.customs.declaration.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDeclarationSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDeclarationSummaryStore()
        val item = newSeasonalDeclarationSummary("code-983", "title-737")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
