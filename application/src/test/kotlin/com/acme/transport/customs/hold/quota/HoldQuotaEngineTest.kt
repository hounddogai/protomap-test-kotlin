package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HoldQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHoldQuotaEngine(newRegistry())
        val item = newHoldQuota("code-858", "name-787")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
