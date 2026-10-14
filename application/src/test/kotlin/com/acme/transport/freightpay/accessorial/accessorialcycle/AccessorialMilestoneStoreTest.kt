package com.acme.transport.freightpay.accessorial.accessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialMilestoneStore()
        val item = newAccessorialMilestone("code-459", "name-676")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
