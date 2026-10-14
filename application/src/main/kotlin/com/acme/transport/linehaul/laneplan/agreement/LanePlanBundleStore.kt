package com.acme.transport.linehaul.laneplan.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan bundles by code. */
interface LanePlanBundleStore {
    fun find(code: String): LanePlanBundle?
    fun save(item: LanePlanBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanBundleStore(
    var items: MutableMap<String, LanePlanBundle>,
    var order: MutableList<String>,
) : LanePlanBundleStore {
    override fun find(code: String): LanePlanBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanBundle) {
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

    override fun list(): MutableList<LanePlanBundle> {
        val out = mutableListOf<LanePlanBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanBundleStore(): LanePlanBundleStore {
    return MemoryLanePlanBundleStore(mutableMapOf<String, LanePlanBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanBundleStore(
    var inner: LanePlanBundleStore,
    var cache: TtlCache<LanePlanBundle>,
    var hits: Int,
    var misses: Int,
) : LanePlanBundleStore {
    override fun find(code: String): LanePlanBundle? {
        val hit = (cache.get(code) as LanePlanBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanBundle> = inner.list()
}

fun newCachedLanePlanBundleStore(inner: LanePlanBundleStore, ttl: Int, clock: Clock): LanePlanBundleStore {
    return CachedLanePlanBundleStore(
        inner = inner,
        cache = newTtlCache<LanePlanBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan bundles. Empty fields match all. */
data class LanePlanBundleQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanBundle>): MutableList<LanePlanBundle> {
        val matched = filter(items) { candidate: LanePlanBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanBundles(store: LanePlanBundleStore, query: LanePlanBundleQuery): MutableList<LanePlanBundle> {
    return query.apply(store.list())
}
