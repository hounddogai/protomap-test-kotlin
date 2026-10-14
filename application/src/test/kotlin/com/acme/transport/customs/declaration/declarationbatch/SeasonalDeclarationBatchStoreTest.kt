package com.acme.transport.customs.declaration.declarationbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDeclarationBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDeclarationBatchStore()
        val item = newSeasonalDeclarationBatch("code-315", "title-684")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
