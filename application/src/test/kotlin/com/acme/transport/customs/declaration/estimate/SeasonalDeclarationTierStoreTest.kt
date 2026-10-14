package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDeclarationTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDeclarationTierStore()
        val item = newSeasonalDeclarationTier("code-241", "title-954")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
