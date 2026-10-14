package com.acme.transport.terminal.appointment.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment rules by code. */
interface AppointmentRuleStore {
    fun find(code: String): AppointmentRule?
    fun save(item: AppointmentRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentRuleStore(
    var items: MutableMap<String, AppointmentRule>,
    var order: MutableList<String>,
) : AppointmentRuleStore {
    override fun find(code: String): AppointmentRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentRule) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<AppointmentRule> {
        val out = mutableListOf<AppointmentRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentRuleStore(): AppointmentRuleStore {
    return MemoryAppointmentRuleStore(mutableMapOf<String, AppointmentRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentRuleStore(
    var inner: AppointmentRuleStore,
    var cache: TtlCache<AppointmentRule>,
    var hits: Int,
    var misses: Int,
) : AppointmentRuleStore {
    override fun find(code: String): AppointmentRule? {
        val hit = (cache.get(code) as AppointmentRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentRule> = inner.list()
}

fun newCachedAppointmentRuleStore(inner: AppointmentRuleStore, ttl: Int, clock: Clock): AppointmentRuleStore {
    return CachedAppointmentRuleStore(
        inner = inner,
        cache = newTtlCache<AppointmentRule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment rules. Empty fields match all. */
data class AppointmentRuleQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentRule>): MutableList<AppointmentRule> {
        val matched = filter(items) { candidate: AppointmentRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentRules(store: AppointmentRuleStore, query: AppointmentRuleQuery): MutableList<AppointmentRule> {
    return query.apply(store.list())
}
