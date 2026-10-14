package com.acme.transport.customs.declaration.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationScheduleStore()
        val item = newDeclarationSchedule("code-507", "name-392")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
