package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial baselines by code. */
interface AccessorialBaselineStore {
    fun find(code: String): AccessorialBaseline?
    fun save(item: AccessorialBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialBaselineStore(
    var items: MutableMap<String, AccessorialBaseline>,
    var order: MutableList<String>,
) : AccessorialBaselineStore {
    override fun find(code: String): AccessorialBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialBaseline) {
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

    override fun list(): MutableList<AccessorialBaseline> {
        val out = mutableListOf<AccessorialBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialBaselineStore(): AccessorialBaselineStore {
    return MemoryAccessorialBaselineStore(mutableMapOf<String, AccessorialBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialBaselineStore(
    var inner: AccessorialBaselineStore,
    var cache: TtlCache<AccessorialBaseline>,
    var hits: Int,
    var misses: Int,
) : AccessorialBaselineStore {
    override fun find(code: String): AccessorialBaseline? {
        val hit = (cache.get(code) as AccessorialBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialBaseline> = inner.list()
}

fun newCachedAccessorialBaselineStore(inner: AccessorialBaselineStore, ttl: Int, clock: Clock): AccessorialBaselineStore {
    return CachedAccessorialBaselineStore(
        inner = inner,
        cache = newTtlCache<AccessorialBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial baselines. Empty fields match all. */
data class AccessorialBaselineQuery(
    var country: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialBaseline): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialBaseline>): MutableList<AccessorialBaseline> {
        val matched = filter(items) { candidate: AccessorialBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialBaselines(store: AccessorialBaselineStore, query: AccessorialBaselineQuery): MutableList<AccessorialBaseline> {
    return query.apply(store.list())
}
