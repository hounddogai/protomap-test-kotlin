package com.acme.transport.customs.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictInspectionProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictInspectionProfileStore()
        val item = newStrictInspectionProfile("code-112", "name-277")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
