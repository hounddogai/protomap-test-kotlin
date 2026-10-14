package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledRestBreakDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledRestBreakDigestEngine(newRegistry())
        val item = newScheduledRestBreakDigest("code-199", "title-306")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
