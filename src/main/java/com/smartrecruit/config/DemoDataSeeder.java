package com.smartrecruit.config;

import com.smartrecruit.model.Job;
import com.smartrecruit.model.JobStatus;
import com.smartrecruit.model.Role;
import com.smartrecruit.model.User;
import com.smartrecruit.repository.JobRepository;
import com.smartrecruit.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Optional: with SEED_DEMO=true and an empty jobs table, adds sample jobs so the board is not empty for a demo. */
@Component
public class DemoDataSeeder implements ApplicationRunner {

    private final UserRepository users;
    private final JobRepository jobs;
    private final PasswordEncoder encoder;
    private final boolean enabled;

    public DemoDataSeeder(UserRepository users, JobRepository jobs, PasswordEncoder encoder,
                          @Value("${app.seed-demo:false}") boolean enabled) {
        this.users = users;
        this.jobs = jobs;
        this.encoder = encoder;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled || jobs.count() > 0) return;
        User hr = new User();
        hr.setName("Demo HR Team");
        hr.setEmail("demo-hr@smartrecruit.local");
        hr.setPasswordHash(encoder.encode(UUID.randomUUID().toString())); // nobody can log in as this account
        hr.setRole(Role.RECRUITER);
        hr.setCreatedAt(Instant.now());
        hr = users.save(hr);

        add(hr, "Java Backend Developer", "Infosys", "Hyderabad", "Junior (0-2 yrs)", "Java, Spring Boot, MySQL, REST",
            "Build and maintain Spring Boot REST services backed by MySQL. Write unit tests, review code and work with the React team.", 1);
        add(hr, "React Frontend Developer", "TCS", "Bengaluru", "Junior (0-2 yrs)", "React, JavaScript, CSS, REST",
            "Create responsive interfaces in React, integrate REST APIs and improve accessibility and performance.", 2);
        add(hr, "Full Stack Engineer", "Wipro", "Chennai", "Mid (2-5 yrs)", "Java, Spring Boot, React, SQL, Docker",
            "Own features end to end: database design, secure APIs and polished UI. Deploy with Docker and CI.", 3);
        add(hr, "Data Analyst", "Accenture", "Pune", "Junior (0-2 yrs)", "SQL, Excel, Python, Power BI",
            "Turn hiring and operations data into dashboards and insights for business teams.", 4);
        add(hr, "DevOps Engineer", "Tech Mahindra", "Remote", "Mid (2-5 yrs)", "Docker, Kubernetes, CI/CD, AWS",
            "Automate builds and deployments, monitor services and keep environments secure.", 5);
        add(hr, "QA Automation Engineer", "Capgemini", "Hyderabad", "Junior (0-2 yrs)", "Selenium, JUnit, Postman, Java",
            "Design automated test suites for web and API features and report quality metrics.", 6);
    }

    private void add(User hr, String title, String company, String location, String level, String skills, String desc, int daysAgo) {
        Job j = new Job();
        j.setRecruiter(hr);
        j.setTitle(title);
        j.setCompany(company);
        j.setLocation(location);
        j.setExperienceLevel(level);
        j.setRequiredSkills(skills);
        j.setDescription(desc);
        j.setStatus(JobStatus.OPEN);
        j.setCreatedAt(Instant.now().minus(daysAgo, ChronoUnit.DAYS));
        jobs.save(j);
    }
}
