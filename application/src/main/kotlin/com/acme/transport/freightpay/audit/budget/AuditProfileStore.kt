package com.acme.transport.freightpay.audit.budget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit profiles by code. */
interface AuditProfileStore {
    fun find(code: String): AuditProfile?
    fun save(item: AuditProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditProfileStore(
    var items: MutableMap<String, AuditProfile>,
    var order: MutableList<String>,
) : AuditProfileStore {
    override fun find(code: String): AuditProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditProfile) {
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

    override fun list(): MutableList<AuditProfile> {
        val out = mutableListOf<AuditProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditProfileStore(): AuditProfileStore {
    return MemoryAuditProfileStore(mutableMapOf<String, AuditProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditProfileStore(
    var inner: AuditProfileStore,
    var cache: TtlCache<AuditProfile>,
    var hits: Int,
    var misses: Int,
) : AuditProfileStore {
    override fun find(code: String): AuditProfile? {
        val hit = (cache.get(code) as AuditProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditProfile> = inner.list()
}

fun newCachedAuditProfileStore(inner: AuditProfileStore, ttl: Int, clock: Clock): AuditProfileStore {
    return CachedAuditProfileStore(
        inner = inner,
        cache = newTtlCache<AuditProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit profiles. Empty fields match all. */
data class AuditProfileQuery(
    var depot: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditProfile): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditProfile>): MutableList<AuditProfile> {
        val matched = filter(items) { candidate: AuditProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditProfiles(store: AuditProfileStore, query: AuditProfileQuery): MutableList<AuditProfile> {
    return query.apply(store.list())
}
