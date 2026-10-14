package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy attempt audits by code. */
interface LegacyAttemptAuditStore {
    fun find(code: String): LegacyAttemptAudit?
    fun save(item: LegacyAttemptAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAttemptAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAttemptAuditStore(
    var items: MutableMap<String, LegacyAttemptAudit>,
    var order: MutableList<String>,
) : LegacyAttemptAuditStore {
    override fun find(code: String): LegacyAttemptAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAttemptAudit) {
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

    override fun list(): MutableList<LegacyAttemptAudit> {
        val out = mutableListOf<LegacyAttemptAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAttemptAuditStore(): LegacyAttemptAuditStore {
    return MemoryLegacyAttemptAuditStore(mutableMapOf<String, LegacyAttemptAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAttemptAuditStore(
    var inner: LegacyAttemptAuditStore,
    var cache: TtlCache<LegacyAttemptAudit>,
    var hits: Int,
    var misses: Int,
) : LegacyAttemptAuditStore {
    override fun find(code: String): LegacyAttemptAudit? {
        val hit = (cache.get(code) as LegacyAttemptAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAttemptAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAttemptAudit> = inner.list()
}

fun newCachedLegacyAttemptAuditStore(inner: LegacyAttemptAuditStore, ttl: Int, clock: Clock): LegacyAttemptAuditStore {
    return CachedLegacyAttemptAuditStore(
        inner = inner,
        cache = newTtlCache<LegacyAttemptAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy attempt audits. Empty fields match all. */
data class LegacyAttemptAuditQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAttemptAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyAttemptAudit>): MutableList<LegacyAttemptAudit> {
        val matched = filter(items) { candidate: LegacyAttemptAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAttemptAudits(store: LegacyAttemptAuditStore, query: LegacyAttemptAuditQuery): MutableList<LegacyAttemptAudit> {
    return query.apply(store.list())
}
