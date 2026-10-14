package com.acme.transport.customs.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryInspectionProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryInspectionProfileStore()
        val item = newPrimaryInspectionProfile("code-670", "label-950")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
