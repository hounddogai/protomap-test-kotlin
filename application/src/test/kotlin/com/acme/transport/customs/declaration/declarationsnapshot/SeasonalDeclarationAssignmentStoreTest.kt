package com.acme.transport.customs.declaration.declarationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDeclarationAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDeclarationAssignmentStore()
        val item = newSeasonalDeclarationAssignment("code-361", "name-416")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
