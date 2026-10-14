package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment proposals by code. */
interface AppointmentProposalStore {
    fun find(code: String): AppointmentProposal?
    fun save(item: AppointmentProposal)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentProposal>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentProposalStore(
    var items: MutableMap<String, AppointmentProposal>,
    var order: MutableList<String>,
) : AppointmentProposalStore {
    override fun find(code: String): AppointmentProposal? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentProposal) {
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

    override fun list(): MutableList<AppointmentProposal> {
        val out = mutableListOf<AppointmentProposal>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentProposalStore(): AppointmentProposalStore {
    return MemoryAppointmentProposalStore(mutableMapOf<String, AppointmentProposal>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentProposalStore(
    var inner: AppointmentProposalStore,
    var cache: TtlCache<AppointmentProposal>,
    var hits: Int,
    var misses: Int,
) : AppointmentProposalStore {
    override fun find(code: String): AppointmentProposal? {
        val hit = (cache.get(code) as AppointmentProposal?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentProposal) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentProposal> = inner.list()
}

fun newCachedAppointmentProposalStore(inner: AppointmentProposalStore, ttl: Int, clock: Clock): AppointmentProposalStore {
    return CachedAppointmentProposalStore(
        inner = inner,
        cache = newTtlCache<AppointmentProposal>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment proposals. Empty fields match all. */
data class AppointmentProposalQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentProposal): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentProposal>): MutableList<AppointmentProposal> {
        val matched = filter(items) { candidate: AppointmentProposal -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentProposals(store: AppointmentProposalStore, query: AppointmentProposalQuery): MutableList<AppointmentProposal> {
    return query.apply(store.list())
}
