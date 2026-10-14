package com.acme.transport.freightpay.fuelsurcharge.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingFuelSurchargeMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingFuelSurchargeMilestoneStore()
        val item = newRollingFuelSurchargeMilestone("code-816", "name-345")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
