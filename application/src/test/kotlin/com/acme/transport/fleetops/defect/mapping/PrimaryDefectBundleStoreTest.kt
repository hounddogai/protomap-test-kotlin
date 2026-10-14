package com.acme.transport.fleetops.defect.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDefectBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDefectBundleStore()
        val item = newPrimaryDefectBundle("code-210", "title-70")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
