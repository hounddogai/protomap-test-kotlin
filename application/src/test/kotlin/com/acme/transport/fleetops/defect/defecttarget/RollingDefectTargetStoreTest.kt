package com.acme.transport.fleetops.defect.defecttarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDefectTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDefectTargetStore()
        val item = newRollingDefectTarget("code-803", "title-361")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
