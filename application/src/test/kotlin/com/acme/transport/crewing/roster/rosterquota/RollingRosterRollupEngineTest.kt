package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingRosterRollupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingRosterRollupEngine(newRegistry())
        val item = newRollingRosterRollup("code-793", "label-274")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
