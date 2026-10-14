package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyClearanceNoticeEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyClearanceNoticeEngine(newRegistry())
        val item = newLegacyClearanceNotice("code-998", "label-225")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
